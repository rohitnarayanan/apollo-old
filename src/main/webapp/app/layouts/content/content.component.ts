import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'awf-content',
  templateUrl: './content.component.html',
  styleUrls: ['./content.component.css']
})

export class ContentComponent implements OnInit {
  pageTitle = 'INDEX';
  pageDescription = 'INDEX PAGE';
  pageControlName = '';
  showPageControl = false;
  handlePageControl = function() {	
  };

  constructor() {}

  ngOnInit() {
  }
}
