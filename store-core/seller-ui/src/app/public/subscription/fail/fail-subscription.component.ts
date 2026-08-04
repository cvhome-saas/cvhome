import {Component, inject} from '@angular/core';
import {NbCardModule} from '@nebular/theme';
import {TranslateModule} from '@ngx-translate/core';
import {PublicNavigationFacade} from '../../shared/facades/public-navigation.facade';

@Component({
  selector: 'app-seller-ui-fail-subscription',
  standalone: true,
  imports: [NbCardModule, TranslateModule],
  providers: [PublicNavigationFacade],
  styles: `
      .flex-centered {
        margin: auto;
      }

      nb-card-body {
        display: flex;
      }

      .title {
        text-align: center;
      }

      .sub-title {
        text-align: center;
        display: block;
        margin-bottom: 3rem;
      }

      .btn {
        margin-bottom: 2rem;
      }
    `,
  template: `
        <div class="row">
            <div class="col-md-12">
                <nb-card status="danger">
                    <nb-card-header>{{ 'SUBSCRIPTION.FAILED_MESSAGE_HEADER' | translate }}</nb-card-header>
                    <nb-card-body>
                        <div class="flex-centered col-xl-4 col-lg-6 col-md-8 col-sm-12">
                            <h2 class="title">{{ 'SUBSCRIPTION.PROCESSING_FAIL' | translate }}</h2>
                            <small class="sub-title">{{ 'SUBSCRIPTION.FAILED_MESSAGE' | translate }}</small>
                            <button (click)="goToHome()" type="button" class="btn btn-block btn-hero-primary">
                                {{ 'NOT_FOUND.BACK' | translate }}
                            </button>
                        </div>
                    </nb-card-body>
                </nb-card>
            </div>
        </div>
    `,
})
export class FailSubscriptionComponent {
  private readonly facade = inject(PublicNavigationFacade);

  goToHome(): void {
    this.facade.goHome();
  }
}
