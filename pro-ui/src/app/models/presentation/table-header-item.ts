import { IDropDownValue } from "../../interfaces/interfaces";

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
    public filterOptions: IDropDownValue[] = [];
    public filterAny: boolean = false;
    public sortDirection: string = ''; // 'asc' or 'desc'

    constructor(label: string | null, name: string | null, filterable: boolean, searchable: boolean, sortable: boolean, selectAll: boolean, filterOptions: IDropDownValue[] | undefined = undefined, filterAny: boolean = false) {
        this.label = label;
        this.name = name;
        this.filterable = filterable;
        this.searchable = searchable;
        this.sortable = sortable;
        this.selectAll = selectAll;
        if (filterOptions) {
            this.filterOptions = filterOptions;
        }
        this.filterAny = filterAny;
    }

}