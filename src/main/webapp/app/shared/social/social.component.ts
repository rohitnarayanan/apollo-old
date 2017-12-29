import {Component, Input, OnInit} from '@angular/core';
import {SocialService} from './social.service';
import {CSRFService} from '../auth/csrf.service';

@Component({
  selector: 'awf-social',
  templateUrl: './social.component.html'
})

export class SocialComponent implements OnInit {
  @Input() provider: string;
  label: string;
  providerSetting: string;
  providerURL: string;
  csrf: string;

  constructor(
    private csrfService: CSRFService,
    private socialService: SocialService
  ) {}

  ngOnInit() {
    this.label = this.provider.charAt(0).toUpperCase() + this.provider.slice(1);
    this.providerSetting = this.socialService.getProviderSetting(this.provider);
    this.providerURL = this.socialService.getProviderURL(this.provider);
    this.csrf = this.csrfService.getCSRF();
  }
}
