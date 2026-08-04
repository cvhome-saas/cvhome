import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {HomeFacade} from './facades/home.facade';
import {ClientDashboardComponent} from './client-dashboard/client-dashboard.component';
import {AdminDashboardComponent} from './admin-dashboard/admin-dashboard.component';

@Component({
  selector: 'ngx-home',
  standalone: true,
  imports: [ClientDashboardComponent, AdminDashboardComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  providers: [HomeFacade]
})
export class HomeComponent implements OnInit {
  protected readonly facade = inject(HomeFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
