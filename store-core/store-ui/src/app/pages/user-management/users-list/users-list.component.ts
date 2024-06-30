import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';

import {UserService} from '../../shared/services/user.service';
import {TranslateService} from '@ngx-translate/core';
import {StorageService} from '../../shared/services/storage.service';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {Page} from "../../shared/models/Page";
import {ShowcaseDialogComponent} from "../../shared/components/showcase-dialog/showcase-dialog.component";
import {NbDialogService, NbToastrService} from "@nebular/theme";


@Component({
  selector: 'ngx-users-list',
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.scss']
})
export class UsersListComponent implements OnInit {
  loadingList = false;
  page: Page = new Page();

  perPage = 15;
  currentPage = 1;
  totalCount;
  totalPages;
  rows: any;
  params = this.loadParams();

  protected readonly ColumnMode = ColumnMode;

  constructor(
    private userService: UserService,
    private router: Router,
    private dialogService: NbDialogService,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private storageService: StorageService) {
  }

  //object
  loadParams() {
    return {
      lang: this.translate.currentLang,
      store: "",
      count: this.perPage,
      page: 0,
    };
  }

  getList() {
    this.params.page = this.currentPage - 1;
    this.loadingList = true;
    this.userService.getUsersList(this.params.store, this.params)
      .subscribe(res => {
        this.rows = res.data.map(user => {
          user.name = user.firstName + ' ' + user.lastName;
          return user;
        });
        this.page.totalPages = 1
        this.page.totalElements = this.rows.length
        this.page.size = this.rows.length
        this.loadingList = false;
      });

  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getList();
  }

  onSelectStore(e) {
    this.params.store = e.id;
    this.setPage({offset: 0});
  }

  ngOnInit() {
    this.translate.onLangChange.subscribe((lang) => {
      this.params.lang = lang.lang;
    });
  }


  onEdit(event) {
    this.router.navigate(['pages/user-management/user/', event.id]);
  }

  onDelete(event) {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    }).onClose.subscribe(res => {
      if (res) {
        this.userService.deleteUser(event.id, this.params.store)
          .subscribe(data => {
            this.toastr.success(this.translate.instant('USER_FORM.USER_REMOVED'));
            this.getList();
          });
      }
    });
  }


  createUser() {
    this.router.navigate(['/pages/user-management/create-user/', this.params.store])
  }
}
