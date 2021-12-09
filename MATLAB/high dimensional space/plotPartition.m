    
number =500;
no_of_discards = 0;
hilbert = false;
pivot = false;


localDists = dists(1:number,:);
d2minusd1 = localDists(:,2) - localDists(:,1);
diffMed = median(d2minusd1);

d1Med = median(localDists(:,1));
d2Med = median(localDists(:,2));

discards = zeros(2,number);
non_discs = zeros(2,number);
for i = 1 : number
    d1 = dists(i,1);
    d2 = dists(i,2);
    discard = false;
    
    %a^2 = b^2 + c^2 - 2bc cosA
    % so.... cosA = (b^2 + c^2 - a^2)/2bc
    x_offset = (d1 * d1 + pDist * pDist - d2 * d2) / (2 * pDist);
    y_offset = sqrt(d1*d1 - x_offset * x_offset);
    
%     if ~pivot && hilbert && abs(x_offset - pDist/2) > threshold
    if ~pivot && hilbert && abs(d1*d1 - d2*d2)/(2*pDist) > threshold
        discard = true;
    else
        if ~pivot && ~hilbert && abs(d1-d2) > threshold * 2
            discard = true;
        else
            if pivot && abs(d1 - d1Med) > threshold
                discard = true;
            end
        end
    end
    
    if discard
        no_of_discards = no_of_discards + 1;
        discards(1,no_of_discards) = x_offset - pDist/2;
        discards(2,no_of_discards) = y_offset;
    else
        non_discs(1,i - no_of_discards) = x_offset - pDist/2;
        non_discs(2,i - no_of_discards) = y_offset;
    end
end

h = figure;
whitespace = 1.1;
axis([min(min(discards(1,:),non_discs(1,:)))*whitespace,max(max(discards(1,:),non_discs(1,:)))*whitespace,0,max(max(discards(2,:),non_discs(2,:)))*whitespace]);
hold on;
p1 = scatter(non_discs(1,1:number - no_of_discards),non_discs(2,1:number - no_of_discards),50,[0,0,0]);
p2 = scatter(discards(1,1:no_of_discards),discards(2,1:no_of_discards),50,[0,0,0],'filled');
p3 = scatter([-pDist/2,pDist/2],[0,0],50,[0,0,0]);

if ~hilbert & ~pivot
    plotHyperbolaLine(pDist/2,threshold,true,true);
    plot(linspace(0,0,10),linspace(0,2,10),'-','Color',[1,0,0]);
end

if hilbert & ~pivot
   plot(linspace(threshold,threshold,10),linspace(0,2,10),'-','Color',[1,0,0]);
   plot(linspace(-threshold,-threshold,10),linspace(0,2,10),'-','Color',[1,0,0]);
end

if pivot
   rads = linspace(0,pi,100);
   xyPlot = zeros(2,100);
   for i = 1 : 100
       xyPlot(1,i) = (d1Med-threshold) * cos(rads(i)) - pDist/2;
       xyPlot(2,i) = (d1Med-threshold) * sin(rads(i));
   end
   plot(xyPlot(1,:),xyPlot(2,:),'-','Color',[1,0,0]);
   for i = 1 : 100
       xyPlot(1,i) = (d1Med+threshold) * cos(rads(i)) - pDist/2;
       xyPlot(2,i) = (d1Med+threshold) * sin(rads(i));
   end
   plot(xyPlot(1,:),xyPlot(2,:),'-','Color',[1,0,0]);
end

% theta = linspace(0,pi()/2,1000);
% circ = linspace(m,m,length(theta));
% inner = linspace(m - threshold ,m - threshold ,length(theta));
% outer = linspace(m + threshold ,m + threshold ,length(theta));

hold off;
title_text = 'Hilbert Exclusion, {}';
if ~hilbert
    title_text = 'Hyperbolic Exclusion, {}';
end
if pivot
    title_text = 'Single Pivot Exclusion, {}';
end


title(strcat(title_text, dataSeriesName), 'FontSize',20);
xlabel('X', 'FontSize',16);
ylabel('altitude from line (p_1,p_2)', 'FontSize',16)

p2_label = strcat('exclusive queries, n = {}',num2str(no_of_discards));
legend([p1,p2],'non-exclusive queries',p2_label)

saveas(h,'lastPartitionPlot.png');