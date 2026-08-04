import {Injectable, inject, signal} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {Router} from '@angular/router';
import {ConfigService} from '../../../shared/services/config.service';
import {StoreService} from '../../services/store.service';
import {ErrorService} from '../../../shared/services/error.service';
import {Pods, PodService} from '../../services/pod.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {StoreFormService} from '../services/store-form.service';
import {forkJoin} from 'rxjs';
import {sideMenuLinks} from '../../services/constents';
import {ReferenceCountry, ReferenceCurrency} from '../../../shared/services/config.service';
import {Language} from '../../../shared/models/Language';
import {PersistableMerchantStore, ReadableMerchantStoreWithPod} from '../../models/store-service.model';

@Injectable()
export class StoreFormFacade {
  private readonly formService = inject(StoreFormService);
  private readonly configService = inject(ConfigService);
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  private readonly errorService = inject(ErrorService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly podService = inject(PodService);

  readonly loader = signal<boolean>(false);
  readonly submitted = signal<boolean>(false);
  readonly isReadonlyName = signal<boolean>(false);
  readonly isReadonlyPodSelection = signal<boolean>(false);
  readonly isNameUnique = signal<boolean>(true);
  readonly selectedItem = signal<string>('5');

  readonly countries = signal<ReferenceCountry[]>([]);
  readonly supportedCurrency = signal<ReferenceCurrency[]>([]);
  readonly supportedTheme = signal<string[]>([]);
  readonly supportedColorThemes = signal<string[]>([]);
  readonly weightList = signal<string[]>([]);
  readonly sizeList = signal<string[]>([]);
  readonly pods = signal<Pods>([]);
  readonly supportedLanguagesList = signal<Language[]>([]);

  supportedLanguagesSelected: string[] = [];
  readonly sideMenuLinks = sideMenuLinks;

  form: FormGroup = this.formService.createForm();

  get name() { return this.form.get('name'); }
  get pod() { return this.form.get('pod')?.get('id'); }
  get phone() { return this.form.get('phone'); }
  get theme() { return this.form.get('theme'); }
  get colorTheme() { return this.form.get('colorTheme'); }
  get address() { return this.form.get('address')?.get('address'); }
  get stateProvince() { return this.form.get('address')?.get('stateProvince'); }
  get country() { return this.form.get('address')?.get('country'); }
  get city() { return this.form.get('address')?.get('city'); }
  get postalCode() { return this.form.get('address')?.get('postalCode'); }
  get email() { return this.form.get('email'); }
  get inBusinessSince() { return this.form.get('inBusinessSince'); }
  get supportedLanguages() { return this.form.get('supportedLanguages'); }
  get defaultLanguage() { return this.form.get('defaultLanguage'); }
  get currency() { return this.form.get('currency'); }
  get weight() { return this.form.get('weight'); }
  get dimension() { return this.form.get('dimension'); }
  get requireLoginForOrderPlacement() { return this.form.get('requireLoginForOrderPlacement'); }

  init(store?: ReadableMerchantStoreWithPod): void {
    this.loader.set(true);
    forkJoin([
      this.configService.getListOfCountries(),
      this.configService.getListOfSupportedCurrency(),
      this.configService.getWeightAndSizes(),
      this.storeService.getSupportedThemes(),
      this.storeService.getSupportedColorThemes(),
      this.podService.listPods()
    ]).subscribe({
      next: ([countries, currencies, measures, themes, colorThemes, pods]) => {
        this.countries.set([...countries]);
        this.supportedCurrency.set([...currencies]);
        this.supportedTheme.set([...themes]);
        this.supportedColorThemes.set([...colorThemes]);
        this.weightList.set([...measures.weights]);
        this.sizeList.set([...measures.measures]);
        this.pods.set(pods);
        this.supportedLanguagesList.set(this.configService.getListOfGlobalLanguages());
        this.loader.set(false);

        if (store && store.id) {
          this.setStore(store);
        }
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  setStore(store: ReadableMerchantStoreWithPod): void {
    if (store && store.id) {
      this.supportedLanguagesSelected = this.formService.fillForm(this.form, store);
      this.isReadonlyName.set(true);
      this.isReadonlyPodSelection.set(true);
    }
  }

  save(storeInput?: ReadableMerchantStoreWithPod): void {
    this.submitted.set(true);
    if (this.form.valid) {
      this.form.controls['address'].patchValue({country: this.form.value.address.country});
      this.form.controls['address'].patchValue({stateProvince: this.form.value.address.stateProvince});
      const storeObj: PersistableMerchantStore = this.form.value;
      storeObj.supportedLanguages = this.supportedLanguagesSelected;

      if (storeInput && storeInput.id) {
        storeObj.id = storeInput.id;
        this.storeService.updateStore(storeObj).subscribe({
          next: () => {
            this.errorService.success('STORE_FORM.STORE_UPDATED');
            this.router.navigate(['pages/store-management/stores-list']);
          },
          error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
        });
      } else {
        this.storeService.checkIfStoreExist(this.form.value.name).subscribe({
          next: (res) => {
            if (res.exists) {
              this.errorService.success('COMMON.NAME_EXISTS');
            } else {
              this.storeService.createStore(storeObj).subscribe({
                next: (createdStore) => {
                  this.selectedStoreService.newStore(createdStore);
                  this.errorService.success('STORE_FORM.STORE_CREATED');
                  this.router.navigate(['pages/store-management/stores-list']);
                },
                error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
              });
            }
          },
          error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
        });
      }
    }
  }

  addSupportedLanguage(languageCode: string): void {
    const selectedIndex = this.supportedLanguagesSelected.indexOf(languageCode);
    if (selectedIndex !== -1) {
      this.supportedLanguagesSelected.splice(selectedIndex, 1);
    } else {
      this.supportedLanguagesSelected.push(languageCode);
    }
    this.form.patchValue({'supportedLanguages': this.supportedLanguagesSelected});
  }

  userHasSupportedLanguage(language: Language, storeInput?: ReadableMerchantStoreWithPod): boolean {
    if (!storeInput || !storeInput.supportedLanguages) {
      return false;
    }
    return storeInput.supportedLanguages.find((l) => l === language.code) !== undefined;
  }

  checkName(_event: Event): void {
    this.storeService.checkIfStoreExist(this.form.value.name).subscribe({
      next: (res) => this.isNameUnique.set(!res.exists),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  route(link: string, storeId: string): void {
    this.router.navigate(['pages/store-management/' + link + '/', storeId]);
  }

  goToBack(): void {
    this.router.navigate(['pages/store-management/stores-list']);
  }
}
