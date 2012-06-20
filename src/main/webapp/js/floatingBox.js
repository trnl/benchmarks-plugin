var
    margin = 20,
    w = 500,
    h = 200;

function drawChart() {
    var e = d3.select(this);
    d3.json('benchmarks/getAll', function (data) {

        var builds = data.map(function (e) {
            return e.build
        });

        // min & max
        var max = d3.max(data, function (d) {
            return d.sum;
        });
//        var min = 0;
        var min = d3.min(data, function (d) {
            return d.sum;
        })

        // scales
        var x = d3.scale.ordinal()
            .domain(builds)
            .rangePoints([0, 450]);
        var y = d3.scale.linear()
            .range([160, 0])
            .domain([min, max]);

        // axes
        var yAxis = d3.svg.axis()
            .scale(y)
            .orient('left')
            .tickSize(-450)
            .tickPadding(5)
            .tickFormat(function (d) {
                var k = d3.format('d')(d);
                return k ? k + 's' : '';
            });
        var xAxis = d3.svg.axis()
            .scale(x)
            .tickSize(159)
            .tickPadding(5);

        // svg elements and styles
        var g = e
            .append('svg:svg')
            .attr('width', w)
            .attr('height', h)
            .append('g')
            .attr('transform', 'translate(' + margin * 2 + ',' + margin * 0.5 + ')');

        // y ticks and labels
        var yTick = g.append('svg:g')
            .call(yAxis);
        var xTick = g.append('svg:g')
            .call(xAxis);

        // rotate x labels
        xTick.selectAll("text")
            .attr("transform", function (d) {
                return "rotate(-90 " + 0 + " " + d3.select(this).attr('y') + "),translate(0, -4)"
            })
            .attr('text-anchor', 'end');


        var area = d3.svg.area()
            .interpolate("linear")
            .x(function (d) {
                return x(d.build);
            })
            .y0(y(min))
            .y1(function (d) {
                return y(d.sum);
            });


        var chart = g.append('svg:g')
            .attr('class', 'line');

        chart
            .append('path')
            .attr('class', 'data-area')
            .style('fill', '#94ca33')
            .style('opacity', 0.8)
            .attr("d", area(data));

        e.select('svg')
            .attr('height', xTick.node().getBoundingClientRect().height + margin)

        e.select('svg')
            .attr('width', yTick.node().getBoundingClientRect().width + margin)
    });


}

d3.select('#f-benchmarks-chart').each(drawChart);