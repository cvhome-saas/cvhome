import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {FilesFacade} from './facades/files.facade';

@Component({
  selector: 'files-content',
  standalone: false,
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
