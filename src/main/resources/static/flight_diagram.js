var DropdownFill = React.createClass({

  getIatas: function () {
    $.ajax({
      url: "http://localhost:8080/iatas"
    }).done(function(data) {
    console.log("ajax call complete, got=" + data)
        this.state = {iatas: data};
      });
  },

  getInitialState: function () {
    return {iatas: []};
  },

  componentDidMount: function () {
    this.getIatas();
  },

  render: function() {
//  var rows = [];
  console.log(this.state)
//      this.state.iatas.forEach(function(iata) {
//        rows.push(<DropdownMenuElement iata={iata}/>);
//      });
//      console.log(rows)
//  return rows;
    return ( <DropdownMenuElement iata={this.state.iatas}/> );
  }
});

//var DropdownMenu = React.createClass({
//render: function() {
//    var rows = [];
//    this.props.iatas.forEach(function(iata) {
//      rows.push(<DropdownMenuElement iata={iata}/>);
//    });
//    return ( <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">{rows}</div>);
//  }
//});

var DropdownMenuElement = React.createClass({
  render: function() {
  console.log("redering element = " + this.props.iata)
    return (<a class="dropdown-item">{this.props.iata}</a>);
  }
});

ReactDOM.render(
  <DropdownFill/>, document.getElementById('dropdownMenu')
);