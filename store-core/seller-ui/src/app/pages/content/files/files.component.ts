import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule} from '@nebular/theme';
import {NgxFileDropModule} from 'ngx-file-drop';
import {LightboxModule} from 'ngx-lightbox';
import {FilesFacade} from './facades/files.facade';

@Component({
  selector: 'app-files-content',
  standalone: true,
  imports: [TranslateModule, NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule, NgxFileDropModule, LightboxModule],
  templateUrl: './files.component.html',
  styleUrls: ['./files.component.scss'],
  providers: [FilesFacade]
})
export class FilesComponent implements OnInit {
  protected readonly facade = inject(FilesFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
