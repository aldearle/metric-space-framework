
number = 500;
no_of_discards = 0;
no_of_excs = 0;
hilbert = true;

discards = zeros(2,number);
non_discs = zeros(2,number);
otherPartitions = zeros(2,number);

for i = 1 : number
    d1 = dists(i,1);
    d2 = dists(i,2);
    discard = false;
    
    %a^2 = b^2 + c^2 - 2bc cosA
    % so.... cosA = (b^2 + c^2 - a^2)/2bc
    cos_piv_1 = (d1 * d1 + pDist * pDist - d2 * d2) / (2 * pDist * d1);
    x_offset = cos_piv_1 * d1;
    y_offset = sqrt(d1*d1 - x_offset * x_offset);
    
    if hilbert && abs(x_offset - pDist/2) > threshold
        discard = true;
    else
        if ~hilbert && abs(d1-d2) > threshold * 2
            discard = true;
        end
    end
    
    d3 = min( dists(i,:));
    if d3 ~= d1 && d3 ~= d2
        no_of_excs = no_of_excs + 1;
        otherPartitions(1,no_of_excs) = x_offset - pDist/2;
        otherPartitions(2,no_of_excs) = y_offset;
    elseif discard
        no_of_discards = no_of_discards + 1;
        discards(1,no_of_discards) = x_offset - pDist/2;
        discards(2,no_of_discards) = y_offset;
    else
        non_discs(1,i - no_of_discards) = x_offset - pDist/2;
        non_discs(2,i - no_of_discards) = y_offset;
    end
end

h = figure;
axis([-1,1,0,2]);
hold on;
p1 = scatter(non_discs(1,1:number - no_of_discards),non_discs(2,1:number - no_of_discards));
p2 = scatter(discards(1,1:no_of_discards),discards(2,1:no_of_discards));
p3 = scatter(otherPartitions(1,1:no_of_excs),otherPartitions(2,1:no_of_excs));
p4 = scatter([-pDist/2,pDist/2],[0,0]);

theta = linspace(0,pi()/2,1000);
circ = linspace(m,m,length(theta));
inner = linspace(m - threshold ,m - threshold ,length(theta));
outer = linspace(m + threshold ,m + threshold ,length(theta));

hold off;
title_text = 'Hilbert Partition, ';
if ~hilbert
    title_text = 'Non-Hilbert Partition, ';
end

title(strcat(title_text, num2str(currentDim), ' dimensions'));
xlabel('distance from centre');
ylabel('altitude from line (p_1,p_2)')
p1_label = strcat('non-exclusive queries, n = ',num2str(number - (no_of_discards + no_of_excs)));
p2_label = strcat('exclusive queries, n = ',num2str(no_of_discards));
legend([p1,p2,p3],p1_label, p2_label, 'other partitions')
pic_filename = 'partition_plot2_';
if hilbert
    pic_filename = strcat(pic_filename,'hilbert_');
else
    pic_filename = strcat(pic_filename,'non_hilbert_');
end
saveas(h,strcat(pic_filename,num2str(currentDim),'.png'));