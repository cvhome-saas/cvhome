import {Component} from '@angular/core';

@Component({
  selector: 'images-table',
  templateUrl: './images.component.html',
  styleUrls: ['./images.component.scss'],
})

export class ImagesComponent {
  store: string = '';
  loadingList = false;

  onSelectStore(e) {
    this.store = e.id
  }

}
