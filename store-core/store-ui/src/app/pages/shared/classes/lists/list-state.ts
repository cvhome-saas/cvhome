export class ListState {
  filterChange: boolean;
  filterResetable: boolean;
  filterString: string;

  constructor(
    fChange: boolean,
    fReseatable: boolean,
    fString: string
  ) {

    this.filterChange = fChange,
      this.filterResetable = fReseatable,
      this.filterString = fString

  }

}
