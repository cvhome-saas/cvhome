import {AfterViewInit, Component, EventEmitter} from '@angular/core';
import {FormControl} from "@angular/forms";
import {StatisticsParams} from "../service/statistic.service";
import {SelectedStoreService} from '../../shared/services/selected-store.service';

@Component({
  selector: 'ngx-client-dashboard',
  standalone: false,
  templateUrl: './client-dashboard.component.html',
  styleUrl: './client-dashboard.component.scss'
})
export class ClientDashboardComponent implements AfterViewInit {
  loading = false;
  loader: boolean = false;
  params: StatisticsParams;
  paramsEmitter: EventEmitter<StatisticsParams> = new EventEmitter();
  fromDateControl: FormControl<Date>;
  toDateControl: FormControl<Date>;
  fromMaxDates: Date = new Date();
  toMaxDates: Date = new Date();


  constructor(private selectedStoreService: SelectedStoreService) {
    this.fromDateControl = new FormControl(this.previousDays(7));
    this.toDateControl = new FormControl(new Date());
    this.params = {
      store: "",
      fromDate: this.fromDateControl.value,
      toDate: this.toDateControl.value
    }
  }

  ngAfterViewInit(): void {
    this.selectedStoreService.current().subscribe((store) => {
      this.params.store = store;
      this.paramsEmitter.emit(this.params)
    })
  }

  previousDays(days: number) {
    let d = new Date();
    d.setDate(d.getDate() - days);
    return d;
  }

  onFromDateChanged(e: Date) {
    this.params.fromDate = e;
    this.paramsEmitter.emit(this.params)
  }

  onToDateChanged(e: Date) {
    this.params.toDate = e;
    this.paramsEmitter.emit(this.params)
  }

}
