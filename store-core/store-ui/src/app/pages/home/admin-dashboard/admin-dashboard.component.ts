import {AfterViewInit, Component, EventEmitter, OnInit} from '@angular/core';
import {StatisticsParams} from "../service/statistic.service";
import {FormControl} from "@angular/forms";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'ngx-admin-dashboard',
  standalone:false,
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit, AfterViewInit {
  loading = false;
  loader: boolean = false;
  params: StatisticsParams;
  paramsEmitter: EventEmitter<StatisticsParams> = new EventEmitter();
  fromDateControl: FormControl<Date>;
  toDateControl: FormControl<Date>;
  fromMaxDates: Date = new Date();
  toMaxDates: Date = new Date();


  constructor(private translate: TranslateService) {
    this.fromDateControl = new FormControl(this.previousDays(7));
    this.toDateControl = new FormControl(new Date());
    this.params = {
      lang: this.translate.currentLang,
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

  ngOnInit() {
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
