import {Component, DestroyRef, Input, OnInit, inject} from '@angular/core';
import {NbDialogRef, NbSpinnerModule} from '@nebular/theme';
import {ImageBrowserFacade} from './facades/image-browser.facade';
import {ContentFileItem} from 'seller-core/content';

@Component({
  selector: 'ngx-image-browser',
  standalone: true,
  imports: [NbSpinnerModule],
  templateUrl: './image-browser.component.html',
  styleUrls: ['./image-browser.component.scss'],
  providers: [ImageBrowserFacade]
})
export class ImageBrowserComponent implements OnInit {
  @Input() store: string;

  protected readonly facade = inject(ImageBrowserFacade);
  protected readonly ref = inject(NbDialogRef<ImageBrowserComponent>);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }

  cancel(): void {
    this.ref.close();
  }

  openImage(value: ContentFileItem): void {
    this.ref.close(value.path + value.name);
  }
}
