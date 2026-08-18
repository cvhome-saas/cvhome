import {FormControl, FormGroup} from '@angular/forms';
import {applyFieldErrors, clearServerErrors, SERVER_ERROR_KEY, serverErrorOf} from './form-error.utils';
import {ProblemFieldError} from './problem-detail.model';

function podForm(): FormGroup {
  return new FormGroup({
    name: new FormControl(''),
    pod: new FormGroup({name: new FormControl('')}),
    endpoint: new FormGroup({endpoint: new FormControl('')}),
  });
}

function fieldError(field: string, code = 'NotBlank'): ProblemFieldError {
  return {field, code, message: 'must not be blank'};
}

describe('applyFieldErrors', () => {

  it('binds a bean path to its control under the server key', () => {
    const form = podForm();

    const unmatched = applyFieldErrors(form, [fieldError('endpoint.endpoint', 'Pattern')]);

    expect(unmatched).toEqual([]);
    expect(serverErrorOf(form.get('endpoint.endpoint'))?.code).toBe('Pattern');
    expect(form.get('endpoint.endpoint')!.hasError(SERVER_ERROR_KEY)).toBeTrue();
  });

  it('carries the whole ProblemFieldError, so code and params survive for translation', () => {
    const form = podForm();
    const error: ProblemFieldError = {field: 'name', code: 'Size', message: 'too long', params: {max: 40}};

    applyFieldErrors(form, [error]);

    expect(serverErrorOf(form.get('name'))).toEqual(error);
  });

  it('resolves a constraint-violation path by dropping the method segment when the full path misses', () => {
    const form = podForm();

    // ConstraintViolationErrorHandler sends `createPod.pod.name`; the control is at `pod.name`.
    const unmatched = applyFieldErrors(form, [fieldError('createPod.pod.name')]);

    expect(unmatched).toEqual([]);
    expect(serverErrorOf(form.get('pod.name'))?.code).toBe('NotBlank');
  });

  it('prefers the full path when one exists, rather than always stripping', () => {
    const form = new FormGroup({
      pod: new FormGroup({name: new FormControl('')}),
      name: new FormControl(''),
    });

    applyFieldErrors(form, [fieldError('pod.name')]);

    expect(serverErrorOf(form.get('pod.name'))).not.toBeNull();
    expect(serverErrorOf(form.get('name'))).toBeNull();
  });

  it('returns what it could not match instead of dropping it', () => {
    const form = podForm();
    const orphan = fieldError('somethingObjectLevel', 'AssertTrue');

    const unmatched = applyFieldErrors(form, [fieldError('name'), orphan]);

    expect(unmatched).toEqual([orphan]);
  });

  it('marks matched controls as touched, since templates gate on dirty || touched', () => {
    const form = podForm();

    applyFieldErrors(form, [fieldError('name')]);

    expect(form.get('name')!.touched).toBeTrue();
  });

  it('keeps client validator errors alongside the server one', () => {
    const form = podForm();
    form.get('name')!.setErrors({required: true});

    applyFieldErrors(form, [fieldError('name')]);

    expect(form.get('name')!.hasError('required')).toBeTrue();
    expect(form.get('name')!.hasError(SERVER_ERROR_KEY)).toBeTrue();
  });

});

describe('clearServerErrors', () => {

  it('removes the server error so the form can become valid again', () => {
    const form = podForm();
    applyFieldErrors(form, [fieldError('name')]);
    expect(form.get('name')!.invalid).toBeTrue();

    clearServerErrors(form);

    expect(form.get('name')!.errors).toBeNull();
    expect(form.get('name')!.valid).toBeTrue();
  });

  it('nulls the errors object rather than leaving it empty', () => {
    // Angular treats `{}` as invalid-with-no-errors, which is a form that can never be submitted.
    const form = podForm();
    applyFieldErrors(form, [fieldError('name')]);

    clearServerErrors(form);

    expect(form.get('name')!.errors).toBeNull();
  });

  it('leaves client validator errors in place', () => {
    const form = podForm();
    form.get('name')!.setErrors({required: true});
    applyFieldErrors(form, [fieldError('name')]);

    clearServerErrors(form);

    expect(form.get('name')!.hasError('required')).toBeTrue();
    expect(form.get('name')!.hasError(SERVER_ERROR_KEY)).toBeFalse();
  });

  it('reaches nested groups', () => {
    const form = podForm();
    applyFieldErrors(form, [fieldError('endpoint.endpoint'), fieldError('pod.name')]);

    clearServerErrors(form);

    expect(serverErrorOf(form.get('endpoint.endpoint'))).toBeNull();
    expect(serverErrorOf(form.get('pod.name'))).toBeNull();
  });

});

describe('serverErrorOf', () => {

  it('is null for a control with no server error, and for no control at all', () => {
    expect(serverErrorOf(new FormControl(''))).toBeNull();
    expect(serverErrorOf(null)).toBeNull();
    expect(serverErrorOf(undefined)).toBeNull();
  });

});
