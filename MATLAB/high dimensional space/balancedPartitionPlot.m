
number = 500;
no_of_discards = 0;
hilbert = false;
plotCr = true;

localDists = dists(1:number,:);
d2minusd1 = localDists(:,2) - localDists(:,1);
diffMed = median(d2minusd1);

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
    x_coord = x_offset - pDist/2;
%     x_coord = (dists(i,1) * dists(i,1) - dists(i,2) * dists(i,2))/(2 * pDist);
    
    if hilbert
        if abs(x_offset - pDist/2) > threshold
            discard = true;
        end
    else
        if  abs(d2minusd1(i) - diffMed) > threshold * 2
            discard = true;
        end
    end
    
    if discard
        no_of_discards = no_of_discards + 1;
        discards(1,no_of_discards) = x_coord;
        discards(2,no_of_discards) = y_offset;
    else
        non_discs(1,i - no_of_discards) = x_coord;
        non_discs(2,i - no_of_discards) = y_offset;
    end
end

h = figure;
set(gca,'FontSize',14);

whitespace = 1.1;
% axis([min(discards(1,:))*whitespace,max(discards(1,:))*whitespace,0,max(non_discs(2,:))*whitespace]);

axis([min(min(discards(1,:),non_discs(1,:)))*whitespace,max(max(discards(1,:),non_discs(1,:)))*whitespace,0,max(max(discards(2,:),non_discs(2,:)))*whitespace]);
hold on;

p1 = scatter(non_discs(1,1:number - no_of_discards),non_discs(2,1:number - no_of_discards),50,[0,0,0]);
p2 = scatter(discards(1,1:no_of_discards),discards(2,1:no_of_discards),50,[0,0,0],'filled');
p3 = scatter([-pDist/2,pDist/2],[0,0],50,[0,0,0]);
% scatter(coords(1,1:number),coords(2,1:number));

if ~hilbert
    
    plotHyperbolaLine(pDist/2,diffMed/2,false,true);
    
    xPoint = threshold + diffMed/2;
    
    plotHyperbolaLine(pDist/2,xPoint,false,true);
    plotHyperbolaLine(pDist/2,diffMed/2 - threshold,false,true);
end

if hilbert
   plot(linspace(threshold,threshold,10),linspace(0,2,10),'-','Color',[1,0,0]);
   plot(linspace(-threshold,-threshold,10),linspace(0,2,10),'-','Color',[1,0,0]);
end

if plotCr
    piv1 = pivots(1,:)
    piv2 = pivots(2,:)
    piv1cr = 0;
    piv2cr = 0;
    for i = 1 : no_of_discards
        point = discards(:,i);
    end
    for i = 1 : number - no_of_discards
        point = non_discs(:,i);
    end
end

% theta = linspace(0,pi()/2,1000);
% circ = linspace(m,m,length(theta));
% inner = linspace(m - threshold ,m - threshold ,length(theta));
% outer = linspace(m + threshold ,m + threshold ,length(theta));

hold off;
title_text = 'Balanced Hilbert Exclusion, {}';
if ~hilbert
    title_text = 'Balanced Hyperbolic Exclusion, {}';
end

title(strcat(title_text, dataSeriesName), 'FontSize',20);
xlabel('X', 'FontSize',16);
ylabel('altitude from line (p_1,p_2)', 'FontSize',16)
p2_label = strcat('exclusive queries, n = {}', num2str(no_of_discards));
legend([p1,p2],'non-exclusive queries',p2_label)
pic_filename = 'balanced_plot2_';
if hilbert
    pic_filename = strcat(pic_filename,'hilbert_');
else
    pic_filename = strcat(pic_filename,'non_hilbert_');
end

saveas(h,'last_balanced.png');