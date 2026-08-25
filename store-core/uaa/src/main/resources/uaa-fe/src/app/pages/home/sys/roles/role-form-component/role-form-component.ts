import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Role, RolesService} from '../services/roles-service';

@Component({
  selector: 'app-role-form-component',
  standalone: false,
  templateUrl: './role-form-component.html',
  styleUrl: './role-form-component.scss',
})
export class RoleFormComponent implements OnInit {
  form!: FormGroup;

  constructor(private fb: FormBuilder, private rolesService: RolesService) {
  }

  private _value: any;

  @Input() set value(v: any) {
    this._value = v;
    if (this.form) {
      this.update(v);
    }
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      id: [''],
      name: ['']
    });
    if (this._value) {
      this.update(this._value);
    }
  }

  submit() {
    if (this.form.value.id) {
      this.rolesService.update(this.form.value.id, this.form.value).subscribe({
        next: (it: Role) => {
          this.update(it);
        }
      });
    } else {
      this.rolesService.save(this.form.value).subscribe({
        next: (it: Role) => {
          this.update(it);
        }
      });
    }
  }

  update(role: Role) {
    if (!role) return;
    this.form.patchValue(role);
  }
}
