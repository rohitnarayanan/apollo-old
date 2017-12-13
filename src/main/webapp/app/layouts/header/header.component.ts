import {Component, OnInit} from '@angular/core';

import {UserService} from '../../shared/user/user.service';

@Component({
  selector: 'awf-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})

export class HeaderComponent implements OnInit {
  title = 'Apollo';
  shortTitle = 'Apollo';
  context = null;

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.initalizeContext();
  }

  initalizeContext(): void {
    this.context = this.userService.getContextInfo();
  }
}
