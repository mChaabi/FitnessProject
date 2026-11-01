import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CoachesPublic } from './coaches-public';

describe('CoachesPublic', () => {
  let component: CoachesPublic;
  let fixture: ComponentFixture<CoachesPublic>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CoachesPublic]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CoachesPublic);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
