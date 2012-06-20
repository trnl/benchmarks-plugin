var color = d3.scale.category20(),
    margin = 40;


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

    var h = 450;
    d3.select('#main-panel').style('height', nest.length * h + 'px');
//    var w = d3.select('#main-panel').property('clientWidth') - margin * 2.5;
    var w = 900;


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

        var g = report
            .append('svg:svg')
            .attr('height', h)
            .attr('width',w + margin*2)
            .append('g')
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
            .rangePoints([0, w]);

        var y = d3.scale.linear().range([h, 0]).domain([min, max]);

        var yAxis = d3.svg.axis().scale(y).orient('left').tickSize(-w).tickPadding(15).tickFormat(function(d){
            return d + 's';
        });
        var xAxis = d3.svg.axis().scale(x).tickSize(h).tickPadding(15);


        // y ticks and labels
        var yTick = g.append('svg:g')
            .attr('class', 'yTick')
            .call(yAxis);
        var xTick = g.append('svg:g')
            .attr('class', 'xTick')
            .call(xAxis);

        // rotate x labels
        xTick.selectAll("text")
            .attr("transform", function (d) {
                return "rotate(-90 " + 0 + " " + d3.select(this).attr('y') + "),translate(0, -4)"
            })
            .attr('text-anchor', 'end');

        var chart = g.append('svg:g')
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

        var median = d3.svg.line()
            .x(function (d) {
                return x(d.build)
            })
            .y(function (d) {
                return y(d['median']);
            })
            .interpolate("linear");


        var area = d3.svg.area()
            .interpolate("linear")
            .x(function (d) {
                return x(d.build);
            })
            .y0(function (d) {
                return d.min ? y(d.min) : h;
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
            .style('stroke-dasharray', '7 3')
            .style('stroke-width', 2)
            .attr("d", percentile90(benchmark.values));


        chart
            .selectAll('.data-median')
            .data([benchmark.values])
            .enter()
            .append('path')
            .attr('class', 'data-median')
            .style('stroke', color(benchmark.key))
            .style('stroke-dasharray', '2 2')
            .style('stroke-width', 2)
            .attr("d", median(benchmark.values));

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
            .attr('tooltip', function (d) {
                var text = "";
                text += "<div class='main'><b>Average:</b>" + d.average + "</div>";
                text += "<div><b>Min:</b>" + d.min + "</div>";
                text += "<div><b>Max:</b>" + d.max + "</div>";
                text += "<div><b>Median:</b>" + d.median + "</div>";
                text += "<div><b>90%:</b>" + d['90percentile'] + "</div>";
                text += "<div><b>Execution time:</b>" + d.executionTime + "</div>";
                text += "<div><b>Invocations:</b>" + d.invocations + "</div>";
                return text;
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

        var legend = g.append('svg:g')
            .attr('class', 'legend')
            .attr('transform', "translate(0," + (xTick.node().getBoundingClientRect().height + margin / 2) + ")");

        var entry = legend
            .selectAll('.legend-entry')
            .data(['average','median','90%'])
            .enter()
            .append('svg:g')
            .attr('class', 'legend-entry');

        entry
            .append('line')
            .attr("x1", 0)
            .attr("y1", 0)
            .attr("x2", 15)
            .attr("y2", 0)
            .style('stroke-width', 2)
            .style('stroke-dasharray', function(d){
                if (d=='median') return '2 2';
                if (d=='90%') return '7 3';
            })
            .style('stroke', color(benchmark.key))


        entry
            .append('text')
            .text(function (d) {
                return d;
            })
            .attr('text-anchor', 'start')
            .attr('dy', '.32em')
            .attr('dx', '17');

        var ypos = 0,
            xpos = 0;

        // Align labels
        entry
            .attr('transform', function (d) {
                var length = d3.select(this).select('text').node().getComputedTextLength() + 30;
                if (w < xpos + length) {
                    xpos = 0;
                    ypos += 30;
                }
                var t = 'translate(' + xpos + ',' + ypos + ')';
                xpos += length
                return t;
            });

        report.select('svg')
            .attr('height', xTick.node().getBoundingClientRect().height + legend.node().getBoundingClientRect().height + margin + 10)

    });

});