import {AbstractControl, AsyncValidatorFn, ValidationErrors} from '@angular/forms';
import {Observable, of} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, first, map, switchMap} from 'rxjs/operators';
import {DnsCheckService} from "../services/dns-check.service"; // Import necessary operators


// Define the interface for the component instance properties the validator needs
export interface StoreDomainComponentValidatorContext {
  podServerDomain: () => string;
  saasProperties: { alis: string, domain: string }; // Include properties needed by podServerDomain
  shortenPodId: string; // Include properties needed by podServerDomain
}


/**
 * Async validator factory to check if a domain points to the pod server domain.
 * @param componentContext - An object providing access to component methods/properties like podServerDomain.
 * @param dnsService - The service used to perform the DNS check.
 */
export function dnsPointsToPodValidatorFactory(
  componentContext: StoreDomainComponentValidatorContext,
  dnsService: DnsCheckService
): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const domainValue = control.value as string;

    // Get the target domain dynamically from the component context
    const targetPodDomain = componentContext.podServerDomain();
    const saasBaseDomain = componentContext.saasProperties?.domain; // Get the base domain for checking subdomains

    // 1. Return null if the control is empty or has sync errors (required, pattern)
    if (!domainValue || control.hasError('required') || control.hasError('pattern')) {
      return of(null);
    }

    // 2. Return a specific error or null if the necessary infrastructure info isn't loaded yet
    // Check if podServerDomain can be generated (i.e., saasProperties and podId are available)
    if (!componentContext.saasProperties || !componentContext.saasProperties.alis || !componentContext.saasProperties.domain || !componentContext.shortenPodId) {
      console.log(componentContext.saasProperties)
      console.warn('DNS Validator: Infrastructure info (saasProperties or podId) not yet fully available.');
      // You can return null, or a specific error indicating it's waiting
      return of({waitingForInfraInfo: true}); // Indicate that validation is pending info
    }


    // 3. Skip validation for platform-managed domains (subdomains or the pod domain itself)
    // This is a heuristic: if it doesn't contain a '.', or ends with the saasBaseDomain, assume it's a platform subdomain/prefix.
    if (
      !domainValue.includes('.') || // Likely a prefix like "mystore"
      domainValue === targetPodDomain || // User entered the pod domain directly
      (saasBaseDomain && domainValue.endsWith(`.${saasBaseDomain}`)) // User entered a full platform subdomain like "mystore.saas.com"
    ) {
      return of(null); // Skip external DNS check for these cases
    }


    // 4. For actual custom domains, perform the async DNS check
    // Use valueChanges to react to input changes, debounce, and switchMap to cancel previous checks
    return control.valueChanges.pipe(
      debounceTime(700), // Wait for user to stop typing (adjust time as needed)
      distinctUntilChanged(), // Only proceed if the value is different from the last emitted value
      switchMap(currentValue => { // Switch to a new observable for the latest value
        // Re-check basic conditions in case value became empty or invalid during debounce
        if (!currentValue || control.hasError('required') || control.hasError('pattern')) {
          return of(null);
        }
        // Re-check platform-managed domain condition for the latest value
        if (
          !currentValue.includes('.') ||
          currentValue === targetPodDomain ||
          (saasBaseDomain && currentValue.endsWith(`.${saasBaseDomain}`))
        ) {
          return of(null);
        }

        // Call the DNS check service
        return dnsService.checkCustomDomainPointsTo(currentValue, targetPodDomain).pipe(
          map(isValid => {
            // If not valid, return the error object
            return isValid ? null : {
              dnsNotPointingToCname: {
                requiredTarget: targetPodDomain,
                domainChecked: currentValue
              }
            };
          }),
          catchError((err) => {
            // Handle errors from the service itself (e.g., network issues)
            console.error('DNS Check Service Error in validator:', err);
            return of({dnsCheckServiceError: true}); // Return a specific error for service issues
          })
        );
      }),
      // Important: Use first() to make the observable complete after the first result
      // This is necessary for Angular's async validators to work correctly.
      first()
    );
  };
}

