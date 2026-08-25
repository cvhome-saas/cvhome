import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {mergeMap} from 'rxjs';
import {UsersService} from '../services/users-service';

@Component({
  selector: 'app-user-edit-component',
  standalone: false,
  templateUrl: './user-edit-component.html',
  styleUrl: './user-edit-component.scss',
})
export class UserEditComponent implements OnInit, AfterViewInit {
  user: any;

  constructor(private usersService: UsersService, private activatedRoute: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.activatedRoute.params.pipe(mergeMap(it => this.usersService.findOne(it.userId)))
      .subscribe(it => {
        this.user = it;
      });

  }

  ngAfterViewInit(): void {
  }

}
