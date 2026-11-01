import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExercicesComponent } from './exercices-component';

describe('ExercicesComponent', () => {
  let component: ExercicesComponent;
  let fixture: ComponentFixture<ExercicesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExercicesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExercicesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
