
// Only for D3.js [uses getAttribute()]
// Checks to has class
// fix: Object #<SVGCircleElement> has no method 'hasClassName'

SVGCircleElement.prototype.hasClassName = function(name){

    var className = this.getAttribute('class');
    if ( className ) {
        var classList = className.toUpperCase().split(' ');
        var nameUpper = name.toUpperCase();

        for ( var i = 0; i < classList.length; i++ ) {
            if ( classList[i] === nameUpper ) return true;
        }
    }
    return false;
};

// -------------------------------------------------------------------------------------

var color = d3.scale.category20(),
    margin = 40,
//    w = d3.select('#main-panel').property('clientWidth') - margin * 2.5,
    w = 900,
    h = 450;

function drawChart() {
    var e = d3.select(this);
    d3.json('getReport?key=' + e.attr('data'), function (data) {

        var builds = data.report.map(function (e) {
            return e.build
        });

        // min & max
        var max = d3.max(data.report, function (d) {
            return d3.max(d.benchmarks, function (b) {
                return b.opsUser;
            });
        });
        var min = 0;

        // scales
        var x = d3.scale.ordinal()
            .domain(builds)
            .rangePoints([0, w]);
        var y = d3.scale.linear()
            .range([h, 0])
            .domain([min, max]);

        // axes
        var yAxis = d3.svg.axis()
            .scale(y)
            .orient('left')
            .tickSize(-w)
            .tickFormat(function (g) {
                return d3.format(".2s")(g)
            })
            .tickPadding(15);
        var xAxis = d3.svg.axis()
            .scale(x)
            .tickSize(h)
            .tickPadding(15);

        // svg elements and styles
        var g = e
            .append('svg:svg')
            .attr('width',w + margin *2)
            .append('g')
            .attr('transform', 'translate(' + margin * 1.5 + ',' + margin * 0.5 + ')');

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

        // lines group
        var lines = g.append('svg:g')
            .attr('class', 'lines');

        //data work
        var benchmarks = d3.merge(data.report.map(function (b) {
            b.benchmarks.forEach(function (e) {
                e.build = b.build;
                e.change = b.change
            });
            return b.benchmarks;
        }));

        var nest = d3.nest()
            .key(function (i) {
                return i.title
            })
            .entries(benchmarks);

        nest.forEach(function (object) {
            var line = lines.append('svg:g')
                .attr('key', object.key);


            var func = d3.svg.line()
                .x(function (d) {
                    return x(d.build)
                })
                .y(function (d) {
                    return y(d.opsUser)
                })
                .interpolate("linear");

            // Draw the lines
            line
                .selectAll('.data-line')
                .data([object.values])
                .enter()
                .append('path')
                .attr('class', 'data-line')
                .style("stroke", color(object.key))
                .attr("d", func(object.values));

            // Draw the points
            line
                .selectAll('.data-point')
                .data(object.values)
                .enter()
                .append('svg:circle')
                .attr('class', 'data-point')
                .attr('tooltip', function (d) {
                    var text = "";
                    text += "<h3>" + d.build + "</h3>";

                    text += "<table>";
                    text += "<tr class=main><th>Ops User</th><td>" + d.opsUser.toFixed(12) + "</td></tr>";
                    text += "<tr><th>Ops Real</th><td>" + d.opsReal.toFixed(12) + "</td></tr>";
                    text += "<tr><th>Time User</th><td>" + d.timeUser.toFixed(6) + "</td></tr>";
                    text += "<tr><th>Time Real</th><td>" + d.timeReal.toFixed(6) + "</td></tr>";
                    text += "<tr><th>Iterations</th><td>" + d.iterations+ "</td></tr>";
                    text += "<tr><th>Rounds</th><td>" + d.rounds+ "</td></tr>";
                    text += "</table>";

                    if ( d.change ) {
                        text += "<table>";
                        text += "<tr><th>Commit</th><td>" + d.change.id+ "</td></tr>";
                        text += "<tr><th>Date</th><td>" + d.change.date + "</td></tr>";
                        text += "<tr><th>Author</th><td>" + d.change.author + "</td></tr>";
                        text += "<tr><th>E-Mail</th><td>" + d.change.email + "</td></tr>";
                        text += "</table>";
                    }

                    return text;
                })
                .attr('cx', function (d) {
                    return x(d.build)
                })

                .style('stroke', color(object.key))
                .attr('cy', function (d) {
                    return y(d.opsUser)
                })
                .attr('r', 4)
                .on('mouseover', function (d) {
                    d3.select(this).transition().style('fill', color(d.title));
                    d3.select(this).attr('tooltip', 'text');
                })
                .on('mouseout', function () {
                    d3.select(this).transition().style('fill', '#fff');
                }).each(function(){
                    var text = this.getAttribute("tooltip");
                    this.onmouseover = function(ev) {
                        var delay = this.getAttribute("nodismiss")!=null ? 99999999 : 5000;
                        tooltip.cfg.setProperty("autodismissdelay",delay);
                        return tooltip.onContextMouseOver.call(this,YAHOO.util.Event.getEvent(ev),tooltip);
                    }
                    this.onmousemove = function(ev) { return tooltip.onContextMouseMove.call(this,YAHOO.util.Event.getEvent(ev),tooltip); }
                    this.onmouseout  = function(ev) { return tooltip.onContextMouseOut .call(this,YAHOO.util.Event.getEvent(ev),tooltip); }
                    this.title = text;
                });
        });

        var legend = g.append('svg:g')
            .attr('class', 'legend')
            .attr('transform', "translate(0," + (xTick.node().getBoundingClientRect().height + margin / 2) + ")");

        var entry = legend
            .selectAll('.legend-entry')
            .data(nest)
            .enter()
            .append('svg:g')
            .attr('class', 'legend-entry')
            .on('click', function (d, i) {
                var line = lines.select('[key="' + d.key + '"]');
                var circle = d3.select(this).select('circle');
                if (line.attr('visibility') == 'hidden') {
                    circle.style('fill', color(d.key));
                    line.attr('visibility', 'visible');
                }
                else {
                    circle.style('fill', '#fff')
                    line.attr('visibility', 'hidden');
                }

            })
            .on('mouseover', function (d, i) {
                lines.selectAll('g:not([key="' + d.key + '"])')
                    .transition().duration(500)
                    .delay(1000)
                    .style('opacity', 0.1)

                lines.select('[key="' + d.key + '"]')
                    .selectAll('circle')
                    .transition()
                    .duration(500)
                    .delay(1000)
                    .style('fill', color(d.key));
            })
            .on('mouseout', function (d, i) {
                lines.selectAll('g:not([key="' + d.key + '"])')
                    .transition().duration(500)
                    .style('opacity', 1)

                lines.select('[key="' + d.key + '"]')
                    .selectAll('circle')
                    .transition().duration(500)
                    .style('fill', '#fff');
            });
        entry
            .append('circle')
            .style('fill', function (d, i) {
                return color(d.key)
            })
            .style('stroke-width', 2)
            .style('stroke', function (d, i) {
                return color(d.key)
            })
            .attr('r', 4);


        entry
            .append('text')
            .text(function (d) {
                return d.key.substring(d.key.lastIndexOf(".") + 1, d.key.length)
            })
            .attr('text-anchor', 'start')
            .attr('dy', '.32em')
            .attr('dx', '8');

        var ypos = 0,
            xpos = 0;

        // Align labels
        entry
            .attr('transform', function (d) {
                var length = d3.select(this).select('text').node().getComputedTextLength() + 30;
                if (w < xpos + length) {
                    xpos = 0;
                    ypos += 20;
                }
                var t = 'translate(' + xpos + ',' + ypos + ')';
                xpos += length
                return t;
            });


        // Update svg height
        e.select('svg')
            .attr('height', xTick.node().getBoundingClientRect().height + legend.node().getBoundingClientRect().height + margin + 10)
    });
}

d3.selectAll('.chart').each(drawChart);