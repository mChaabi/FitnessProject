import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PricingPublic } from './pricing-public';

describe('PricingPublic', () => {
  let component: PricingPublic;
  let fixture: ComponentFixture<PricingPublic>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PricingPublic]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PricingPublic);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
