import {Injectable, inject, signal} from '@angular/core';
import {FormArray, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../../services/store.service';
import {ErrorService} from '../../../shared/services/error.service';
import {StoreLandingPageFormService} from '../services/store-landing-page.form.service';
import {forkJoin} from 'rxjs';
import {sideMenuLinks} from '../../services/constents';

@Injectable()
export class StoreLandingPageFacade {
  private readonly formService = inject(StoreLandingPageFormService);
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly errorService = inject(ErrorService);

  readonly loader = signal<boolean>(false);
  readonly store = signal<any>(null);
  readonly languages = signal<any[]>([]);
  readonly selectedItem = signal<string>('1');
  readonly page = signal<any>(null);
  readonly sideMenuLinks = sideMenuLinks;

  form: FormGroup = this.formService.createForm();

  get selectedLanguage() {
    return this.form.get('selectedLanguage');
  }

  get descriptions(): FormArray {
    return this.form.get('descriptions') as FormArray;
  }

  init(): void {
    const storeCode = this.activatedRoute.snapshot.paramMap.get('code');
    if (!storeCode) return;

    this.loader.set(true);
    forkJoin([
      this.storeService.getPageContent(storeCode, 'LANDING_PAGE'),
      this.storeService.getStore(storeCode)
    ]).subscribe({
      next: ([res, st]) => {
        if (!res.status) {
          this.page.set(res);
        }
        this.store.set(st);
        const langs = st.supportedLanguages || [];
        this.languages.set(langs);
        this.form = this.formService.createForm();
        this.formService.addFormArray(this.form, langs);
        this.formService.fillForm(this.form, this.page());
        this.loader.set(false);
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  route(link: string): void {
    const st = this.store();
    if (st) {
      this.router.navigate(['pages/store-management/' + link + '/', st.id]);
    }
  }

  save(): void {
    const st = this.store();
    const pg = this.page();
    if (!st) return;

    this.form.patchValue({name: st.name});
    this.form.patchValue({code: 'LANDING_PAGE'});

    if (pg && pg.id) {
      this.storeService.updatePageContent(st.id, pg.id, this.form.value).subscribe({
        next: () => {
          this.errorService.success('STORE_LANDING.PAGE_UPDATED');
        },
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
      });
    } else {
      this.storeService.createPageContent(this.form.value, st.id).subscribe({
        next: () => {
          this.errorService.success('STORE_LANDING.PAGE_ADDED');
          this.router.navigate(['pages/store-management/store/', st.id]);
        },
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
      });
    }
  }
}
