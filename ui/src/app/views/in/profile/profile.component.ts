import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../../service/auth.service";
import {AuthUser} from "../../../model/auth-user";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  authUser: AuthUser | null = null;

  constructor(private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.getAuthUser().subscribe(it => {
      this.authUser = it
    })
  }
}
