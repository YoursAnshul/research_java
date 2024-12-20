export class TableHeaderItem {

    public label: string | null = '';
    public name: string | null = '';
    public filterable: boolean = false;
    public searchable: boolean = false;
    public sortable: boolean = false;
    public selectAll: boolean = false;

    // ui states
    public searchValue: string = '';
    public filterValue: any[] = [];
    public sortDirection: string = ''; // 'asc' or 'desc'

    constructor(label: string | null, name: string | null, filterable: boolean, searchable: boolean, sortable: boolean, selectAll: boolean) {
        this.label = label;
        this.name = name;
        this.filterable = filterable;
        this.searchable = searchable;
        this.sortable = sortable;
        this.selectAll = selectAll;
    }

}