import {Component, DestroyRef, Input, OnInit, inject} from '@angular/core';
import {NbDialogRef} from '@nebular/theme';
import {BrowsableImage, ImageBrowserFacade} from './facades/image-browser.facade';

@Component({
  selector: 'ngx-image-browser',
  standalone: false,
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

  openImage(value: BrowsableImage): void {
    this.ref.close(value.path + value.name);
  }
}
