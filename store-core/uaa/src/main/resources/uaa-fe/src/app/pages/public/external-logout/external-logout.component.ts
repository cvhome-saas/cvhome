import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-external-logout',
  template: ''
})
export class ExternalLogoutComponent implements OnInit {

  constructor() {
  }

  ngOnInit(): void {
    window.location.href = '/logout';
  }

}
