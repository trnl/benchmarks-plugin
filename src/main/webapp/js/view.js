var color = d3.scale.category20(),
    margin = 40;

var w = d3.select('#main-panel').property('clientWidth') - margin - 20,
    h = 470;


d3.json('getReport?key=' + d3.select('.report-title').text(), function (data) {
    var benchmarks = d3.merge(data.map(function (b) {
        b.benchmarks.forEach(function (e) {
            e.build = b.build
        });
        return b.benchmarks;
    }));


    var nest = d3.nest()
        .key(function (i) {
            return i.title
        })
        .entries(benchmarks);

    nest.forEach(function (benchmark) {
        var report = d3.select('#main-panel')
            .append('div')
            .attr('class', 'report');
        report
            .append('h2')
            .text(benchmark.key.substring(benchmark.key.lastIndexOf(".") + 1, benchmark.key.length));
        report
            .append('div')
            .classed('chart');


        var svg = report
            .append('svg:svg')
            .attr('width', w)
            .attr('height', h)
            .attr('class', 'viz')
            .append('svg:g')
            .attr('transform', 'translate(' + margin * 1.5 + ',' + margin * 0.5 + ')');


        var builds = benchmark.values.map(function (e) {
            return e.build
        });

        var max = d3.max(benchmark.values, function (e) {
            return e.max;
        });

        var min = d3.min(benchmark.values, function (e) {
            var m = d3.min([e.average, e.min, e['90percentile'], e.median]);
            return m ? m : 0;
        });

        var x = d3.scale
            .ordinal()
            .domain(builds)
            .rangePoints([0, w - margin * 2]);

        var y = d3.scale.linear().range([h - margin * 2, 0]).domain([min, max]);

        var yAxis = d3.svg.axis().scale(y).orient('left').tickSize(-w + margin * 2).tickPadding(15);
        var xAxis = d3.svg.axis().scale(x).tickSize(-h + margin * 2).tickPadding(15);


//        y ticks and labels
        svg.append('svg:g')
            .attr('class', 'yTick')
            .call(yAxis)
            .append('text');
//            .text('ms')
//            .attr('text-anchor', 'middle')
//            .attr('transform','rotate(270) translate(-390,-40)');

        //   x ticks and labels
        //   x ticks and labels
        var xTick = svg.append('svg:g')
            .attr('class', 'xTick');

        xTick.attr("transform", "translate(0," + (h - margin * 2) + ")")
            .call(xAxis)
            .selectAll("text")
            .attr("transform", "rotate(-90),translate(-10,-20)")
            .attr('text-anchor', 'end');

        var chart = svg.append('svg:g')
            .attr('class', 'line')
            .attr('key', benchmark.key);


        var average = d3.svg.line()
            .x(function (d) {
                return x(d.build)
            })
            .y(function (d) {
                return y(d.average)
            })
            .interpolate("linear");

        var percentile90 = d3.svg.line()
            .x(function (d) {
                return x(d.build)
            })
            .y(function (d) {
                return y(d['90percentile']);
            })
            .interpolate("linear");


        var area = d3.svg.area()
            .interpolate("linear")
            .x(function (d) {
                return x(d.build);
            })
            .y0(function (d) {
                return d.min ? y(d.min) : h - margin * 2;
            })
            .y1(function (d) {
                return y(d.max);
            });


        // Draw the lines
        chart
            .selectAll('.data-line')
            .data([benchmark.values])
            .enter()
            .append('path')
            .attr('class', 'data-line')
            .style("stroke", color(benchmark.key))
            .attr("d", average(benchmark.values));

        chart
            .selectAll('.data-90')
            .data([benchmark.values])
            .enter()
            .append('path')
            .attr('class', 'data-90')
            .style('stroke', color(benchmark.key))
            .style('stroke-dasharray', '8 4')
            .style('stroke-width', 2)
            .attr("d", percentile90(benchmark.values));

        // Draw the area

        chart
            .selectAll('.data-area')
            .data([benchmark.values])
            .enter()
            .append('path')
            .attr('class', 'data-area')
            .style('fill', color(benchmark.key))
            .style('opacity', 0.2)
            .attr("d", area(benchmark.values));

        // Draw the points
        chart
            .selectAll('.data-point')
            .data(benchmark.values)
            .enter()
            .append('svg:circle')
            .attr('class', 'data-point')
            .attr('cx', function (d) {
                return x(d.build)
            })

            .style('stroke', color(benchmark.key))
            .attr('cy', function (d) {
                return y(d.average)
            })
            .attr('r', 4)
            .on('mouseover', function (d) {
                d3.select(this).transition().style('fill', color(d.title));
                d3.select(this).attr('tooltip', 'text');
            })
            .on('mouseout', function () {
                d3.select(this).transition().style('fill', '#fff');
            });

    });

});