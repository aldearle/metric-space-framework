
number = 500;
no_of_discards = 0;

discards = zeros(2,number);
non_discs = zeros(2,number);

distSums = dists(1:number,1) + dists(1:number,2);
e0 = median(distSums)/2;
e1 = sqrt(e0^2 - (pDist/2)^2);

for i = 1 : number
    discard = false;
    
    d1 = dists(i,1);
    d2 = dists(i,2);
    
    %a^2 = b^2 + c^2 - 2bc cosA
    % so.... cosA = (b^2 + c^2 - a^2)/2bc
    cos_piv_1 = (d1^2 + pDist^2 - d2^2) / (2 * pDist * d1);
    x_offset = cos_piv_1 * d1;
    y_offset = sqrt(d1*d1 - x_offset * x_offset);
    
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
hold on;
%axis([-1,1,0,3]);
p1 = scatter(non_discs(1,1:number - no_of_discards),non_discs(2,1:number - no_of_discards));
p2 = scatter(discards(1,1:no_of_discards),discards(2,1:no_of_discards));
p3 = scatter([-pDist/2,pDist/2],[0,0]);
%scatter(coords(1,:),coords(2,:));

plotEllipseLine(e0,e1);
plotEllipseLine(e0 + threshold,e1 + threshold);
plotEllipseLine(e0 - threshold,e1 - threshold);

title(strcat('Ellipse Exclusions, ', num2str(currentDim), ' dimensions'));
xlabel('x');
ylabel('y')
p2_label = strcat('exclusive queries, n = ',num2str(no_of_discards));
legend([p1,p2],'non-exclusive queries',p2_label)
saveas(h,strcat('ellipse_plot_hilbert_',num2str(currentDim),'.png'));


%
% figure;
% hold on;
% scatter(coords(1,:),coords(2,:));
%
% inner = linspace(m - threshold ,m - threshold ,length(theta));
% outer = linspace(m + threshold ,m + threshold ,length(theta));
% polar(theta,inner);
% polar(theta,outer);
hold off;