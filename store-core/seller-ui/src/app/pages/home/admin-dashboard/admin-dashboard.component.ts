import {AfterViewInit, Component, EventEmitter} from '@angular/core';
import {StatisticsParams} from "../service/statistic.service";
import {FormControl} from "@angular/forms";

@Component({
  selector: 'ngx-admin-dashboard',
  standalone: false,
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent implements AfterViewInit {
  loading = false;
  loader: boolean = false;
  params: StatisticsParams;
  paramsEmitter: EventEmitter<StatisticsParams> = new EventEmitter();
  fromDateControl: FormControl<Date>;
  toDateControl: FormControl<Date>;
  fromMaxDates: Date = new Date();
  toMaxDates: Date = new Date();


  constructor() {
    this.fromDateControl = new FormControl(this.previousDays(7));
    this.toDateControl = new FormControl(new Date());
    this.params = {
      store: "",
      fromDate: this.fromDateControl.value,
      toDate: this.toDateControl.value
    }
  }

  previousDays(days: number) {
    let d = new Date();
    d.setDate(d.getDate() - days);
    return d;
  }

  ngAfterViewInit(): void {
    this.paramsEmitter.emit(this.params)
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
