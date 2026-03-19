import {Component, Input, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup} from '@angular/forms';
import {User, UsersService} from '../services/users-service';
import {RolesService} from '../../roles/services/roles-service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-user-form-component',
  standalone: false,
  templateUrl: './user-form-component.html',
  styleUrl: './user-form-component.scss',
})
export class UserFormComponent implements OnInit {
  form!: FormGroup;
  roles: string[] = [];
  passwordForm!: FormGroup;
  rolesForm!: FormGroup;
  metadataForm!: FormArray;
  private _value: any;

  constructor(private fb: FormBuilder, private usersService: UsersService, private rolesService: RolesService, private router: Router) {
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      id: [''],
      username: [''],
      email: ['']
    });
    this.passwordForm = this.fb.group({
      password: ['']
    });
    this.rolesForm = this.fb.group({
      roles: [[]]
    });
    this.metadataForm = this.fb.array([]);

    if (this._value) {
      this.patchForm(this._value);
    }

    this.rolesService.list({page: 0, count: 1000}).subscribe(page => {
      this.roles = page.content.map(r => r.name);
    });
  }

  @Input() set value(v: any) {
    this._value = v;
    if (this.form) {
      this.patchForm(v);
    }
  }

  patchForm(v: any) {
    if (!v) return;
    this.form.patchValue(v);
    this.rolesForm.patchValue({roles: v.roles});
    this.metadataForm.clear();
    if (v.metadata) {
      Object.keys(v.metadata).forEach(key => {
        this.metadataForm.push(this.fb.group({
          key: [key],
          value: [v.metadata[key]]
        }));
      });
    }
  }


  submit() {
    const metadata: { [key: string]: any } = {};
    this.metadataForm.value.forEach((item: any) => {
      if (item.key) {
        metadata[item.key] = item.value;
      }
    });

    const payload = {...this.form.value, metadata};

    if (this.form.value.id) {
      this.usersService.update(this.form.value.id, payload).subscribe({
        next: (it: User) => {
          this.update(it);
        }
      });

    } else {
      this.usersService.save(payload).subscribe({
        next: (it: User) => {
          this.router.navigate(["/home/users/edit", it.id])
        }
      });

    }
  }

  update(user: User) {
    this.form.patchValue(user);
    this.rolesForm.patchValue({roles: user.roles});
    this.metadataForm.clear();
    if (user.metadata) {
      Object.keys(user.metadata).forEach(key => {
        this.metadataForm.push(this.fb.group({
          key: [key],
          value: [user.metadata[key]]
        }));
      });
    }
  }

  addMetadata() {
    this.metadataForm.push(this.fb.group({
      key: [''],
      value: ['']
    }));
  }

  removeMetadata(index: number) {
    this.metadataForm.removeAt(index);
  }

  getGroup(control: any): FormGroup {
    return control as FormGroup;
  }

  resetPassword() {
    if (this.form.value.id) {
      this.usersService.resetPassword(this.form.value.id, this.passwordForm.value.password).subscribe();
    }
  }

  assignRoles() {
    if (this.form.value.id) {
      this.usersService.assignRoles(this.form.value.id, this.rolesForm.value.roles).subscribe();
    }
  }
}
