import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'awf-content',
  templateUrl: './content.component.html',
  styleUrls: ['./content.component.css']
})

export class ContentComponent implements OnInit {
  pageTitle = 'INDEX';
  pageDescription = 'INDEX PAGE';
  pageControlName = '^BTN';
  showPageControl = false;
  handlePageControl = function() {
    alert('handlePageControl-app');
  };

  constructor() {}

  ngOnInit() {
  }
}
