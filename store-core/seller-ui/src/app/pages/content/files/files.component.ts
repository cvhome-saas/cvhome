import {Component, OnInit, inject} from '@angular/core';
import {FilesFacade, FileImage} from './facades/files.facade';
import {NgxFileDropEntry} from 'ngx-file-drop';

@Component({
  selector: 'files-content',
  standalone: false,
  templateUrl: './files.component.html',
  styleUrls: ['./files.component.scss'],
  providers: [FilesFacade]
})
export class FilesComponent implements OnInit {
  protected readonly facade = inject(FilesFacade);

  get store() { return this.facade.store(); }
  get loader() { return this.facade.loader(); }
  get files() { return this.facade.files(); }
  get images() { return this.facade.images(); }

  ngOnInit(): void {
    this.facade.init();
  }

  removeImage(e: any): void {
    this.facade.removeImage(e);
  }

  openImage(data: FileImage[], index: number): void {
    this.facade.openImage(data, index);
  }

  close(): void {
    this.facade.close();
  }

  unSelectImage(i: number): void {
    this.facade.unSelectImage(i);
  }

  dropped(files: NgxFileDropEntry[]): void {
    this.facade.dropped(files);
  }

  upload(): void {
    this.facade.upload();
  }
}
