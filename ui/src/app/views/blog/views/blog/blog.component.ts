import {Component} from '@angular/core';
import {environment} from "../../../../../environment";

@Component({
    selector: 'app-blog',
    templateUrl: './blog.component.html',
    styleUrls: ['./blog.component.css']
})
export class BlogComponent {
    loginUrl: string;

    constructor() {
      // return this.http.get<User>("/user/api/v1/user", {
      //   headers: {
      //     "Map-Host-As-Request-Param": "true"
      //   }
      // })

      this.loginUrl = environment.LOGIN_URL;
    }
}
