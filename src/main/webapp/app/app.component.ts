import {Component, OnInit, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'awf-app',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})

export class AppComponent implements OnInit {
  encapsulation: ViewEncapsulation.None;

  constructor() {}

  ngOnInit() {
  }
}
