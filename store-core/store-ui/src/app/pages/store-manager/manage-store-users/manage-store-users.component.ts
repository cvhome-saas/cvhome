import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {Group, ManagerStoreId, User} from "../../../shared/domain/commons";
import {UserService} from "../../../shared/service/user.service";

@Component({
  selector: 'app-manage-store-users',
  templateUrl: './manage-store-users.component.html',
  styleUrls: ['./manage-store-users.component.css']
})
export class ManageStoreUsersComponent implements OnInit, OnDestroy {
  currentStoreId: string = '';
  users: Users[] = [];
  groups: string[] = Object.values(Group)
  userForm: FormGroup;


  constructor(private activatedRoute: ActivatedRoute, private userService: UserService) {
    this.userForm = new FormGroup({
      username: new FormControl(null, [
        Validators.required,
        Validators.minLength(4),
        Validators.pattern('^[a-zA-Z0-9]+$')
      ]),
      password: new FormControl(null, [
        Validators.required,
        Validators.minLength(4),
        Validators.pattern('^[a-zA-Z0-9]+$')
      ]),
      groups: new FormControl(null, [
        Validators.required
      ])
    });

  }


  ngOnInit() {

  }

  ngAfterViewInit(): void {

  }

  ngOnDestroy(): void {
  }

  onSubmit() {
    if (this.userForm.valid) {
      let value: any = this.userForm.value;
      this.userService.create({
        username: value.username,
        password: value.password,
        groups: value.groups
      }, {id: this.currentStoreId})
        .subscribe((it: User) => {
          if (!this.users) this.users = []
          this.users.push(this.toUsers(it));
          this.userForm.reset();
        });
    }

  }

  statusChange(id: string, event: Event) {
    if ((<HTMLInputElement>event.target).checked) {
      this.userService.enable({id: this.currentStoreId}, id).subscribe()
    } else {
      this.userService.disable({id: this.currentStoreId}, id).subscribe()
    }
  }

  deleteUser(userId: string) {
    this.userService.delete({id: this.currentStoreId}, userId).subscribe(() => {
      this.users = this.users.filter(u => u.id != userId)
    })
  }

  toUsers(user: User): Users {
    return {
      ...user,
      selectedGroups: Object.values(Group).map(it => {
        return {selected: user.groups.findIndex(e => e === it) != -1, group: it} as SelectedGroups;
      })
    }
  }

  groupsChanged(userId: string, $event: Event) {
    let selectedOptions = (<HTMLSelectElement>$event.target).selectedOptions;
    let groups: Group[] = [];
    for (let i: number = 0; i < selectedOptions.length; i++) {
      let item = selectedOptions.item(i);
      if (item) {
        groups.push(item.value as Group);
      }
    }
    this.userService.updateGroups({id: this.currentStoreId}, userId, {groups}).subscribe(() => {
    })
  }

  storeSelectedChanged($event: ManagerStoreId) {
    this.currentStoreId = $event.id
    this.userService.list($event).subscribe(it => {
      this.users = it.map(x => this.toUsers(x))
    })
  }
}

interface Users extends User {
  selectedGroups?: SelectedGroups[]
}

interface SelectedGroups {
  selected: boolean,
  group: Group,
}
