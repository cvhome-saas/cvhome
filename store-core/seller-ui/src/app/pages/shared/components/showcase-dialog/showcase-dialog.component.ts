import {Component, Input, inject} from '@angular/core';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbDialogRef} from '@nebular/theme';

@Component({
  selector: 'ngx-showcase-dialog',
  standalone: true,
  imports: [TranslateModule, NbButtonModule, NbCardModule],
  templateUrl: 'showcase-dialog.component.html',
  styleUrls: ['showcase-dialog.component.scss'],
})
export class ShowcaseDialogComponent {

  @Input() title: string;
  @Input() text: string;
  @Input() actionText: string;

  protected readonly ref = inject(NbDialogRef<ShowcaseDialogComponent>);
  private readonly translate = inject(TranslateService);

  constructor() {
    this.actionText = this.translate.instant('COMMON.REMOVE_GEN_QUESTION');
  }

  dismiss() {
    this.ref.close();
  }

  remove() {
    this.ref.close(true);
  }

}
