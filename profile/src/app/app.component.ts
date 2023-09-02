import {Component, OnInit} from '@angular/core';
import {UserService} from "./service/user.service";
import {User} from "./domain/user";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
    title = 'profile';
    user: User | undefined;

    constructor(private userService: UserService) {
    }

    ngOnInit(): void {
        this.userService.getUser().subscribe(it => {
            this.user = it
        });
    }

}
