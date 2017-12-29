import {Component, OnInit} from '@angular/core';
import {NgbModalRef} from '@ng-bootstrap/ng-bootstrap';

import {ContentComponent} from '../layouts';
import {Account, LoginModalService, Principal} from '../shared';

@Component({
  selector: 'awf-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})

export class HomeComponent implements OnInit {
  pageContentText = 'Home Content';
  account: Account;
  modalRef: NgbModalRef;

  constructor(
    private contentComponent: ContentComponent,
    private principal: Principal,
    private loginModalService: LoginModalService
  ) {
  }

  ngOnInit() {
    //this.contentComponent.pageControlName = 'TEST';
    //this.contentComponent.showPageControl = true;
    this.contentComponent.pageTitle = "Home";
    this.contentComponent.pageDescription = 'This application provides a set of utilities to migrate your' +
      'application to any target App Server, Version Control, or Build Tool';
    //this.contentComponent.handlePageControl = function() {
    //  alert('handlePageControl-test');
    //};

    this.principal.identity().then((account) => {
      this.account = account;
    });
  }

  isAuthenticated() {
    return this.principal.isAuthenticated();
  }

  login() {
    this.modalRef = this.loginModalService.open();
  }
}
