import {Component, Input} from '@angular/core';
import {NbDialogRef} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'ngx-showcase-dialog',
  standalone:false,
  templateUrl: 'showcase-dialog.component.html',
  styleUrls: ['showcase-dialog.component.scss'],
})
export class ShowcaseDialogComponent {

  @Input() title: string;
  @Input() text: string;
  @Input() actionText: string ;

  constructor(protected ref: NbDialogRef<ShowcaseDialogComponent>, private translate: TranslateService) {
    this.actionText = this.translate.instant('COMMON.REMOVE_GEN_QUESTION');
  }


  dismiss() {
    this.ref.close();
  }

  remove() {
    this.ref.close(true);
  }

}
