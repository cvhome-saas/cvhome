import {Component, Input, OnInit} from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {ClientsService, ClientSummary} from '../services/clients-service';
import {Router} from '@angular/router';

export const redirectUrisValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const grantTypes = control.get('authorizationGrantTypes');
  const redirectUris = control.get('redirectUris');

  if (!grantTypes || !redirectUris) {
    return null;
  }

  const grantTypesValue = grantTypes.value as string[];
  const redirectUrisValue = redirectUris.value as string[];

  const needsRedirectUris = grantTypesValue && grantTypesValue.includes('authorization_code');
  const hasRedirectUris = redirectUrisValue && redirectUrisValue.filter(uri => uri && uri.trim() !== '').length > 0;

  if (needsRedirectUris && !hasRedirectUris) {
    return {redirectUrisRequired: true};
  }

  return null;
};

export const uriValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value;
  if (!value || value.trim() === '') {
    return null;
  }
  // Regex to check if it starts with a scheme and has no spaces.
  const uriRegex = /^[a-zA-Z][a-zA-Z0-9+.-]*:\/\/[^\s]*$/;
  if (!uriRegex.test(value)) {
    return {'invalidUri': true};
  }
  return null;
};


@Component({
  selector: 'app-client-form-component',
  standalone: false,
  templateUrl: './client-form-component.html',
  styleUrl: './client-form-component.scss',
})
export class ClientFormComponent implements OnInit {
  form!: FormGroup;
  resetSecretForm!: FormGroup;
  private _value: any;

  // Options for the select inputs
  clientAuthenticationMethodsOptions: string[] = [];
  authorizationGrantTypesOptions: string[] = [];
  scopesOptions: string[] = [];
  idTokenSignatureAlgorithmOptions: string[] = [];
  tokenEndpointAuthenticationSigningAlgorithmOptions: string[] = [];
  accessTokenFormatOptions: string[] = [];

  constructor(private fb: FormBuilder, private clientsService: ClientsService, private router: Router) {
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      id: [''],
      clientId: ['', Validators.required],
      clientName: [''],

      clientAuthenticationMethods: [["client_secret_basic"]],
      authorizationGrantTypes: [[], [Validators.required, Validators.minLength(1)]],

      redirectUris: this.fb.array([]),
      postLogoutRedirectUris: this.fb.array([]),

      scopes: [[]],

      clientSettings: this.fb.group({
        requireProofKey: [false],
        requireAuthorizationConsent: [false],
        jwkSetUrl: [''],
        tokenEndpointAuthenticationSigningAlgorithm: ["RS256"],
        x509CertificateSubjectDN: [''],
        customSettings: this.fb.group({})
      }),

      tokenSettings: this.fb.group({
        accessTokenTimeToLive: ['PT24H'],
        refreshTokenTimeToLive: ['PT30M'],
        reuseRefreshTokens: [false],
        idTokenSignatureAlgorithm: ["RS256"],
        authorizationCodeTimeToLive: ['PT24H'],
        deviceCodeTimeToLive: ['PT5M'],
        x509CertificateBoundAccessTokens: [false],
        accessTokenFormat: this.fb.group({
          value: ['self-contained']
        }),
        customSettings: this.fb.group({})
      })
    }, {validators: redirectUrisValidator});

    this.resetSecretForm = this.fb.group({
      newSecret: ['', [Validators.required]]
    });

    if (this._value) {
      this.patchForm(this._value);
    }

    this.clientsService.getOptions().subscribe(options => {
      this.clientAuthenticationMethodsOptions = options.clientAuthenticationMethods;
      this.authorizationGrantTypesOptions = options.authorizationGrantTypes;
      this.scopesOptions = options.scopes;
      this.idTokenSignatureAlgorithmOptions = options.idTokenSignatureAlgorithm;
      this.tokenEndpointAuthenticationSigningAlgorithmOptions = options.tokenEndpointAuthenticationSigningAlgorithm;
      this.accessTokenFormatOptions = options.accessTokenFormat;
    });
  }

  @Input() set value(v: any) {
    this._value = v;
    if (this.form) {
      this.patchForm(v);
    }
  }

  patchForm(v: any) {
    if (!v) return;

    this.form.patchValue(v);

    this.redirectUris.clear();
    let redirectUris = v.redirectUris || [];
    if (redirectUris.length == 0) redirectUris.push('')
    redirectUris.forEach((x: string) => this.redirectUris.push(this.createControl(x)));

    this.postLogoutRedirectUris.clear();
    let postLogoutRedirectUris = v.postLogoutRedirectUris || [];
    if (postLogoutRedirectUris.length == 0) postLogoutRedirectUris.push('')
    postLogoutRedirectUris.forEach((x: string) => this.postLogoutRedirectUris.push(this.createControl(x)));

    this.patchCustomSettings('clientSettings', v.clientSettings?.customSettings);
    this.patchCustomSettings('tokenSettings', v.tokenSettings?.customSettings);
  }

  patchCustomSettings(groupName: string, customSettings: any) {
    const group = this.form.get(groupName)?.get('customSettings') as FormGroup;
    if (!group) return;

    Object.keys(group.controls).forEach(key => group.removeControl(key));
    if (customSettings) {
      Object.keys(customSettings).forEach(key => {
        group.addControl(key, new FormControl(customSettings[key]));
      });
    }
  }

  getCustomSettings(groupName: string): string[] {
    const group = this.form.get(groupName)?.get('customSettings') as FormGroup;
    return group ? Object.keys(group.controls) : [];
  }

  addCustomSetting(groupName: string, key: string) {
    if (!key || key.trim() === '') return;
    const group = this.form.get(groupName)?.get('customSettings') as FormGroup;
    if (group && !group.contains(key)) {
      group.addControl(key, new FormControl(''));
    }
  }

  removeCustomSetting(groupName: string, key: string) {
    const group = this.form.get(groupName)?.get('customSettings') as FormGroup;
    if (group) {
      group.removeControl(key);
    }
  }

  createControl(value: string = ''): FormControl {
    return this.fb.control(value, [uriValidator]);
  }

  get redirectUris() {
    return this.form.get('redirectUris') as FormArray;
  }

  get postLogoutRedirectUris() {
    return this.form.get('postLogoutRedirectUris') as FormArray;
  }

  addRedirectUri() {
    this.redirectUris.push(this.createControl(''));
  }

  removeRedirectUri(i: number) {
    this.redirectUris.removeAt(i);
  }

  addPostLogoutUri() {
    this.postLogoutRedirectUris.push(this.createControl());
  }

  removePostLogoutUri(i: number) {
    this.postLogoutRedirectUris.removeAt(i);
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const formValue = {...this.form.value};
    formValue.redirectUris = (formValue.redirectUris || []).filter((uri: string) => uri && uri.trim() !== '');
    formValue.postLogoutRedirectUris = (formValue.postLogoutRedirectUris || []).filter((uri: string) => uri && uri.trim() !== '');

    if (formValue.id) {
      this.clientsService.update(formValue.id, formValue).subscribe({
        next: (it: ClientSummary) => {
          this.update(it);
        }
      });

    } else {
      this.clientsService.save(formValue).subscribe({
        next: (it: ClientSummary) => {
          this.router.navigate(["/home/clients/edit", it.id]);
        }
      });
    }
  }

  resetSecret() {
    if (this.resetSecretForm.invalid) {
      this.resetSecretForm.markAllAsTouched();
      return;
    }
    const newSecret = this.resetSecretForm.value.newSecret;
    this.clientsService.resetSecret(this.form.value.id, newSecret).subscribe(() => {
      this.resetSecretForm.reset();
    });
  }

  update(clientSummary: ClientSummary) {
    this.form.patchValue(clientSummary);
  }

}
