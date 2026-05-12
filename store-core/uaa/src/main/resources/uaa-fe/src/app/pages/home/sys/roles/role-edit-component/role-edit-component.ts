import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {mergeMap} from 'rxjs';
import {RolesService} from '../services/roles-service';

@Component({
  selector: 'app-role-edit-component',
  standalone: false,
  templateUrl: './role-edit-component.html',
  styleUrl: './role-edit-component.scss',
})
export class RoleEditComponent implements OnInit, AfterViewInit {
  role: any;

  constructor(private rolesService: RolesService, private activatedRoute: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.activatedRoute.params.pipe(mergeMap(it => this.rolesService.findOne(it.roleId)))
      .subscribe(it => {
        this.role = it;
      });

  }

  ngAfterViewInit(): void {
  }

}
