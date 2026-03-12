import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ClientsService} from '../services/clients-service';
import {ActivatedRoute} from '@angular/router';
import {mergeMap} from 'rxjs';

@Component({
  selector: 'app-client-edit-component',
  standalone: false,
  templateUrl: './client-edit-component.html',
  styleUrl: './client-edit-component.scss',
})
export class ClientEditComponent implements OnInit, AfterViewInit {
  client: any;
  constructor(private clientService: ClientsService, private activatedRoute: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.activatedRoute.params.pipe(mergeMap(it => this.clientService.findOne(it.clientId)))
      .subscribe(it => {
        this.client = it;
      });

  }

  ngAfterViewInit(): void {
  }

}
