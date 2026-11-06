import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanningPublic } from './planning-public';

describe('PlanningPublic', () => {
  let component: PlanningPublic;
  let fixture: ComponentFixture<PlanningPublic>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanningPublic]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanningPublic);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
