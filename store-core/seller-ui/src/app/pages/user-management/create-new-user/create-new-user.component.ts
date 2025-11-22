import {Component, OnInit} from '@angular/core';

import {User} from '../../shared/models/user';
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'ngx-create-new-user',
  standalone:false,
  templateUrl: './create-new-user.component.html',
  styleUrls: ['./create-new-user.component.scss']
})
export class CreateNewUserComponent implements OnInit {
  user: User;
  store: string = '';

  constructor(private activatedRoute: ActivatedRoute) {
  }

  ngOnInit() {
    this.activatedRoute.params.subscribe(it => {
      this.store = it['store']
    })
  }

}
