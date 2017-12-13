import {Component, OnInit} from '@angular/core';
import {
  ProfileInfoService,
  ProfileInfo
} from '../../shared';

@Component({
  selector: 'awf-page-ribbon',
  template: `<div class="ribbon" *ngIf="ribbonEnv"><a href="">{{ribbonEnv}}</a></div>`,
  styleUrls: [
    'ribbon.component.scss'
  ]
})

export class RibbonComponent implements OnInit {
  profileInfo: ProfileInfo;
  ribbonEnv: string;

  constructor(private profileService: ProfileInfoService) {}

  ngOnInit() {
    this.profileInfo = this.profileService.getProfileInfo();
    if (!this.profileInfo.inProduction) {
      this.ribbonEnv = this.profileInfo.ribbonEnv;
    }
  }
}
