import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProgramForm } from './program-form';

describe('ProgramForm', () => {
  let component: ProgramForm;
  let fixture: ComponentFixture<ProgramForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProgramForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProgramForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
