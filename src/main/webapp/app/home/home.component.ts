import {Component, OnInit} from '@angular/core';

import {ContentComponent} from '../layouts';

@Component({
  selector: 'awf-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})

export class HomeComponent implements OnInit {
  pageContentText = 'Home Content';

  constructor(
    private contentComponent: ContentComponent
  ) {}

  ngOnInit() {
    this.contentComponent.pageControlName = 'NONE';
    this.contentComponent.showPageControl = false;
    this.contentComponent.pageDescription = 'This application provides a set of utilities to migrate your' +
      'application to any target App Server, Version Control, or Build Tool';
    this.contentComponent.handlePageControl = function() {
      alert('handlePageControl-home');
    };
  }
}
